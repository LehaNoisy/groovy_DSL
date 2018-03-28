tURL = "https://github.com/LehaNoisy/groovy_DSL.git"
def git = "LehaNoisy/groovy_DSL"
def repo = "master"

job("main-build-job"){
    description ('Building necessary jobs')
    
parameters {
     choiceParam('BRANCH_NAME', ['ashumilov', 'master'], 'make your choice')
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
        github 'LehaNoisy/groovy_DSL','$BRANCH_NAME'
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


//creating child jobs
    1.upto(4) {
        job("EPBYMINW2470/MNTLAB-ashumilov-child${it}-build-job") {
            description 'Echo the shell.sh.'
            parameters {
                choiceParam('BRANCH_NAME', branches, 'Select git branch')
                /*gitParam('BRANCH_NAME') {
                    description('branch selection')
                    type('BRANCH')
                    //branch('~ /*')
                    //defaultValue('/ashumilov')
                    sortMode('ASCENDING')
                }*/
            }
            scm {
                git {
                    remote {
                        name('mntlab-dsl')
                        url('https://github.com/LehaNoisy/groovy_DSL.git')
                    }
                    branch('$BRANCH_NAME')
                }
                triggers {
                    scm 'H/5 * * * *'
                }
            }
            steps {
                shell('chmod +x script.sh && ./script.sh > output.txt && cat output.txt && ' +
                        'tar -czf ${BRANCH_NAME}_dsl_script.tar.gz output.txt jobs.groovy')
            }
            publishers {
                archiveArtifacts('${BRANCH_NAME}_dsl_script.tar.gz')
            }
        }
    }
}
}
