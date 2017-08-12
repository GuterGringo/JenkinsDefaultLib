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
