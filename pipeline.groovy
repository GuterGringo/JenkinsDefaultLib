@Library('DefaultLib')
import main.defaultlib

def String srcsoap = 'https://github.com/GuterGringo/CustomerMngtSOAPjava.git'
def String soapstash = 'soap'
def String dockerreposoap = 'customermngt'

def String srcgui = 'https://github.com/GuterGringo/MicroserviceUI.git'
def String webstash = 'web'
def String dockerrepoweb = 'webui'

def String srcrest = 'https://github.com/GuterGringo/ProductinfoRESTpython.git'
def String reststash = 'rest'
def String dockerreporest = 'products'

def String dockerhost = '192.168.178.31'
def String testhost = '192.168.178.32'
def globalLib = new defaultlib()


stage "Commit Stage"
parallel 'soap-ws': {
    node {
        globalLib.getSource(srcsoap, soapstash)
    }
}, 
'rest-ws': {
    node {
        globalLib.getSource(srcrest, reststash)
    }
}, 
'web-gui': {
    node {
        globalLib.getSource(srcgui, webstash)
    }
} 

stage "publish images"
parallel 'customermngt': {
    node {
        globalLib.publishImage(soapstash, dockerreposoap, dockerhost)
    }
}, 
'products': {
    node {
        globalLib.publishImage(reststash, dockerreporest, dockerhost)
    }
}, 
'webui': {
    node {
        globalLib.publishImage(webstash, dockerrepoweb, dockerhost)
    }
} 

stage "Acceptance Test Stage"
node {
    globalLib.deployContainer(testhost)
    sh "curl --data '' http://${testhost}/sites/addcustomer.php"
}

stage "Performance Tests" 
node{
    sh "ab -n 5000 -c 100 http://${testhost}/sites/index.php"
    sh "docker-compose -H tcp://${testhost}:2375 down"
}

stage "Production" 
node{
    globalLib.deployContainer(dockerhost)
}
