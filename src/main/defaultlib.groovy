package main;

// get sourcecode from git-repository
def getSource(srcurl, stashname){
	git url:srcurl
	stash includes: '**', name: stashname
}

// create and upload images to docker-repository
def publishImage(stashname, dockerrepo, dockerhost){
	unstash stashname
        sh "docker -H tcp://${dockerhost}:2375 build -t gutergringo/${dockerrepo} ."
        sh "docker -H tcp://${dockerhost}:2375 push gutergringo/${dockerrepo}"	
}

// start docker container on target system
def deployContainer(dockerhost){
	def String srcdockercompose = 'https://github.com/GuterGringo/demoapplication.git'
	git url:srcdockercompose
    	sh "docker-compose -H tcp://${dockerhost}:2375 pull"
    	sh "docker-compose -H tcp://${dockerhost}:2375 up -d --build"
}

// build and test Java JAR file from git-repository
def defaultJarBuild(srcurl, stashname){
	def mvnHome = tool 'M3'
        git url:srcurl
        sh "'${mvnHome}/bin/mvn' -B clean install"
        
	// Unit Test
        sh "${mvnHome}/bin/mvn test"
        junit 'target/surefire-reports/*.xml'
        archive 'target/*.jar'
        stash includes: '**', name: stashname
}
