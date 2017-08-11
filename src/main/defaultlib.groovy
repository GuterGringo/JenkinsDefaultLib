package main;

def mavenbuild(giturl){
	echo 'Hello'
}

def getSource(srcurl, stashname){
	git url:srcurl
	stash includes: '*', name: stashname
}

def getSource(stashname, reponame){
	sh "'${reponame}/bin/mvn' -B clean install"
	unstash stashname
        sh "docker build -t gutergringo/${reponame} ."
        sh "docker push gutergringo/${reponame}"	
}
