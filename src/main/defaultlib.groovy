package main;

def mavenbuild(giturl){
	echo 'Hello'
}

def getSource(srcurl, stashname){
	git url:srcurl
	stash includes: '**', name: stashname
}

def publishImage(stashname, dockerrepo){
	unstash stashname
        sh "docker build -t gutergringo/${dockerrepo} ."
        sh "docker push gutergringo/${dockerrepo}"	
}
