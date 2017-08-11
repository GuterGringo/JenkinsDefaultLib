package main;

def mavenbuild(giturl){
	echo 'Hello'
}

def getSource(srcurl, stashname){
	git url:srcurl
	stash includes: '*', name: stashname
}

def getSource(stashname, dockerrepo){
	unstash stashname
        sh "docker build -t gutergringo/${dockerrepo} ."
        sh "docker push gutergringo/${dockerrepo}"	
}
