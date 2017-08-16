package main;

def getSource(srcurl, stashname){
	git url:srcurl
	stash includes: '**', name: stashname
}

def publishImage(stashname, dockerrepo, dockerhost){
	unstash stashname
        sh "docker -H tcp://${dockerhost}:2375 build -t gutergringo/${dockerrepo} ."
        sh "docker -H tcp://${dockerhost}:2375 push gutergringo/${dockerrepo}"	
}

def deployContainer(dockerhost){
	def String srcdockercompose = 'https://github.com/GuterGringo/demoapplication.git'
	git url:srcdockercompose
    	sh "docker-compose -H tcp://${dockerhost}:2375 pull"
    	sh "docker-compose -H tcp://${dockerhost}:2375 up -d --build"
}

def defaultJarBuild(srcurl, stashname){
	def mvnHome = tool 'M3'
        // Get code from GitHub repository
        git url:srcurl
        sh "'${mvnHome}/bin/mvn' -B clean install"
        
	// Unit Test
        sh "${mvnHome}/bin/mvn test"
        junit 'target/surefire-reports/*.xml'
        archive 'target/*.jar'
        stash includes: '**', name: stashname
}






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
