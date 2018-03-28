def command = "git ls-remote -h https://github.com/LehaNoisy/groovy_DSL.git" as java.lang.Object
def proc = command.execute()
proc.waitFor()
if ( proc.exitValue() != 0 ) {
    println "Error, ${proc.err.text}"
    System.exit(-1)
}
idef branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
}
job('EPBYMINW2470/MNTLAB-ashumilov-DSL-build-job') {
    description 'Create child jobs.'
    parameters {
        //choiceParam(String parameterName, List<String> options, String description)
        choiceParam('BRANCH_NAME', ['aaksionkin', 'master'])
        activeChoiceParam('BUILDS_TRIGGER') {
            description('Available options')
            choiceType('CHECKBOX')
            groovyScript {
                script('["MNTLAB-ashumilov-child1-build-job", "MNTLAB-ashumilov-child2-build-job", "MNTLAB-ashumilov-child3-build-job", "MNTLAB-ashumilov-child4-build-job"]')
            }
        }
    }
        triggers {
            scm('H/5 * * * *')
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
        //shell('chmod +x script.sh && ./script.sh > output.txt && cat output.txt && tar -czf ${BRANCH_NAME}_dsl_script.tar.gz output.txt')
    }
    publishers {
        archiveArtifacts('output.txt')
    }
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
                        name('groovy_DSL')
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

