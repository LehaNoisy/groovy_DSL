def gitURL = "https://github.com/LehaNoisy/groovy_DSL.git"
def git = "LehaNoisy/groovy_DSL.git"
def repo = "LehaNoisy"

job("main-build-job"){
    description ('Building necessary jobs')
    
parameters {
     choiceParam('BRANCH_NAME', ['ashumilov', 'master'], 'choice')
        activeChoiceParam('BUILDS_TRIGGER'){ 
        description('Allows user choose')
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
  


steps {
        downstreamParameterized {
            trigger('$BUILDS_TRIGGER') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    currentBuild()
                }
            }
        }
    }

    publishers {
        archiveArtifacts('output.txt')
    }
}
