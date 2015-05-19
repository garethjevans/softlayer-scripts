@Grapes(
	@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
)
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON
import groovy.json.*

api_key = System.getProperty('apiKey')
api_user = System.getProperty('apiUser')
node = System.getProperty('node')

assert api_key != null
assert api_user != null
assert node != null

client = new RESTClient("https://${api_user}:${api_key}@api.softlayer.com/rest/v3/")

def getAccount() {
	def resp = client.get( path : 'SoftLayer_Account.json' )
	assert resp.status == 200
	resp.getData()
}

def listVirtualServers() {
	def resp = client.get( path : "SoftLayer_Account/VirtualGuests.json" )
	assert resp.status == 200
	resp.getData()
}

def getVirtualServer(String id) {
	def resp = client.get(path: "SoftLayer_Virtual_Guest/${id}.json",
		query: [objectMask: 'id;hostname;provisionDate;primaryIpAddress;primaryBackendIpAddress;operatingSystem.passwords']
	)
	assert resp.status == 200
	resp.getData()
}

def createNode(String nodeName) {
	def json = new JsonBuilder()
	json.call(
		parameters: [
			{
				hostname(nodeName)
				domain('softlayer.com')
				startCpus(8)
				maxMemory('16384')
				datacenter() {
					name('dal05')
				}
				hourlyBillingFlag(true)
				localDiskFlag(true)
				dedicatedAccountHostOnlyFlag(false)
				operatingSystemReferenceCode('CENTOS_LATEST')
				networkComponents([{
					maxSpeed(1000) 
				}])
				postInstallScriptUri('https://raw.githubusercontent.com/garethjevans/provision-scripts/master/install-centos.sh') 
			}
		]
	)	

	println json.toString()

	def resp = client.post(path: "SoftLayer_Virtual_Guest.json",
		requestContentType: JSON,
		body: json.toString()		
	)
	assert resp.status == 201
	resp.getData()
}

String serverId
servers = listVirtualServers()
if ( ! servers.collect { it.hostname }.contains( node ) ) {
	println "Creating node ${node}..."
	serverId = createNode(node).id
} else {
	serverId = servers.find{ it.hostname == node }.id
	println "Found ${serverId}"	
}
server = getVirtualServer(serverId)

while (server.provisionDate == null ) {
	println "Waiting..."
	Thread.sleep(30000)
	server = getVirtualServer(serverId)
}

serverId = server.id 
primaryIpAddress = server.primaryIpAddress
primaryBackendIpAddress = server.primaryBackendIpAddress
username = 'root'
password = server.operatingSystem.passwords[0].password

assert serverId != ''
assert primaryIpAddress != ''
assert primaryBackendIpAddress != ''
assert username != ''
assert password != ''

println ""
println "try ssh ${username}@${primaryIpAddress} - password ${password}"
