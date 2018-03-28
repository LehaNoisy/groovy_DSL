def gitURL = "https://github.com/LehaNoisy/groovy_DSL.git"
def git = "LehaNoisy/groovy_DSL"
def repo = "ashumilov"

def command = "git ls-remote -h $gitURL"

def proc = command.execute()
proc.waitFor()

if ( proc.exitValue() != 0 ) {
    println "Error, ${proc.err.text}"
    System.exit(-1)
}

def branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
}

job("MNTLAB-ashumilov-main-build-job"){
    description ('Building necessary jobs')
    
parameters {
     choiceParam('BRANCH_NAME', ['ashumilov', 'master'], 'Select the branch')
        activeChoiceParam('BUILDS_TRIGGER') {
            description('Available options')
            choiceType('CHECKBOX')
            groovyScript {
                script('["MNTLAB-ashumilov-child1-build-job", "MNTLAB-ashumilov-child2-build-job", "MNTLAB-ashumilov-child3-build-job", "MNTLAB-ashumilov-child4-build-job"]')
            }
        }
    }

scm {
        github(git, '$BRANCH_NAME')
}
    
triggers {
        scm('H/5 * * * *')
}

steps {
    downstreamParameterized {
        trigger('$BUILDS_TRIGGER') {
            block {
                buildStepFailure('FAILURE')
                failure('FAILURE')
                unstable('UNSTABLE')
            }    
            parameters{
                currentBuild()
            }
        }
    }   
    shell('chmod +x script.sh && ./script.sh > output.txt && cat output.txt && tar -czf ${BRANCH_NAME}_dsl_script.tar.gz output.txt')
}
publishers { 
  archiveArtifacts('output.txt')
}
}

1.upto(4){
job("MNTLAB-ashumilov-child${it}-build-job") {
    description "Creating children jobs"
    parameters {
    choiceParam('BRANCH_NAME', branches)
    }
    scm {
        github(git, '$BRANCH_NAME')
    }
steps {    
    copyArtifacts('MNTLAB-ashumilov-main-build-job') {
        includePatterns('script.sh')
        targetDirectory('./')
        flatten()
        optional()
        buildSelector {
        latestSuccessful(true)
        }
    }
}
steps {
    shell('chmod +x script.sh && ./script.sh > output.txt && cat output.txt && tar -czf  ${BRANCH_NAME}_dsl_script.tar.gz output.txt jobs.groovy script.sh')
}
publishers {
        archiveArtifacts {
            pattern('output.txt')
            pattern('${BRANCH_NAME}_dsl_script.tar.gz')
            onlyIfSuccessful()
            }
        }
    }
}
