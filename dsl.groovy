job('EPBYMINW2470/MNTLAB-ashumilov-main-build-job'){
  scm {
        github ('LehaNoisy/groovy_DSL.git', '$BRANCH_NAME')
     }      
  parameters {
        choiceParam('BRANCH_NAME', ['ashumilov', 'master'], 'select branch')
	activeChoiceParam('BUILDS_TRIGGER') {
            choiceType('CHECKBOX')
            groovyScript {
                script('return ["MNTLAB-ashumilov-child1-build-job", "MNTLAB-ashumilov-child2-build-job", "MNTLAB-ashumilov-child3-build-job", "MNTLAB-ashumilov-child4-build-job"]')
            }
        }
    }
  steps {	
	downstreamParameterized {   
                trigger('$BUILDS_TRIGGER'){      
                	block{
                    	buildStepFailure('FAILURE')
                    	failure('FAILURE')
                    	unstable('UNSTABLE')
                }
        parameters {
                    predefinedProp ('BRANCH_NAME', '$BRANCH_NAME')
        }
      }
    }
  }
}


def gitURL = "https://github.com/LehaNoisy/groovy_DSL.git"
def command = "git ls-remote -h -t $gitURL"
def proc = command.execute()
def branches = proc.in.text.readLines().collect
        {
            it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
        }

for (i = 1; i <5; i++) {
  job ("EPBYMINW2470/MNTLAB-ashumilov-child${i}-build-job"){ 
    parameters {
        choiceParam('BRANCH_NAME', branches)
    }		   
    scm {
         github ('MNT-Lab/mntlab-dsl', '$BRANCH_NAME')
    }
    steps {
        shell('chmod +x ./script.sh; ./script.sh >> output.txt; tar -czvf "${BRANCH_NAME}_dsl_script.tar.gz" output.txt script.sh jobs.groovy')    	
 }
    publishers {
        archiveArtifacts {
          pattern('output.txt')
          pattern ('${BRANCH_NAME}_dsl_script.tar.gz')
          onlyIfSuccessful()          
       }
     }
   }
}
