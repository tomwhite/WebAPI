package org.ohdsi.webapi.study;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@Transactional
//@Ignore
public class CreateStudyTest {

  @Autowired
  private StudyRepository studyRepository;

  @Autowired
  private StudyCohortRepository studyCohortRepository;

  @Autowired
  private StudyIRRepository studyIRRepository;

  @Autowired
  private StudyCCARepository studyCCARepository;

  @Autowired
  private StudySCCRepository studySCCRepository;
  
  @PersistenceContext
  protected EntityManager entityManager;
  
  @Test
  @Commit
  public void createStudy() {
    // Create Cohorts

    StudyCohort i_cohort = new StudyCohort();
    i_cohort.setName("Study1: Cohort 4 (Indication)");
    i_cohort.setExpression("{\"someField\": \"someValue\"}");
    i_cohort = studyCohortRepository.save(i_cohort);

    StudyCohort t_cohort = new StudyCohort();
    t_cohort.setName("Study1: Cohort 1 (Target)");
    t_cohort.setExpression("{\"someField\": \"someValue\"}");
    t_cohort = studyCohortRepository.save(t_cohort);

    // Set up relationships
    CohortRelationship rel = new CohortRelationship();
    rel.setTarget(i_cohort);
    rel.setRelationshipType(RelationshipType.INDICATION);
    t_cohort.getCohortRelationships().add(rel);
    
    t_cohort = studyCohortRepository.save(t_cohort);
    
    StudyCohort o_cohort = new StudyCohort();
    o_cohort.setName("Study1: Cohort 2 (Outcome)");
    o_cohort.setExpression("{\"someField\": \"someValue\"}");
    o_cohort = studyCohortRepository.save(o_cohort);

    StudyCohort c_cohort = new StudyCohort();
    c_cohort.setName("Study1: Cohort 3 (Comparator)");
    c_cohort.setExpression("{\"someField\": \"someValue\"}");
    c_cohort = studyCohortRepository.save(c_cohort);

    // create three negative controls
    List<StudyCohort> negativeControls = new ArrayList<>();
    StudyCohort nc;
    
    nc = new StudyCohort();
    nc.setName("Study1: NC 1");
    nc.setExpression("{\"someField\": \"someValue\"}");
    negativeControls.add(studyCohortRepository.save(nc));

    nc = new StudyCohort();
    nc.setName("Study1: NC 2");
    nc.setExpression("{\"someField\": \"someValue\"}");
    negativeControls.add(studyCohortRepository.save(nc));
    
    nc = new StudyCohort();
    nc.setName("Study1: NC 3");
    nc.setExpression("{\"someField\": \"someValue\"}");
    negativeControls.add(studyCohortRepository.save(nc));

    // Create IR Analysis
    
    StudyIR ira = new StudyIR();
    ira.setParams("{\"param1\": \"someValue\"}");
    List<StudyCohort> targets = new ArrayList<>();
    targets.add(t_cohort);
    ira.setTargets(targets);
    
    List<StudyCohort> outcomes = new ArrayList<>();
    outcomes.add(o_cohort);
    ira.setOutcomes(outcomes);
    
    ira = studyIRRepository.save(ira);
    
    // Create CCA
    StudyCCA cca = new StudyCCA();
    StudyCCATrio ccaTrio = new StudyCCATrio();
    ccaTrio.setTarget(t_cohort);
    ccaTrio.setComparator(c_cohort);
    ccaTrio.setOutcome(o_cohort);
    ccaTrio.setCca(cca); // add ref pair -> cca
    // associate first and second NC with this trio
    ccaTrio.getNegativeControls().add(negativeControls.get(0));
    ccaTrio.getNegativeControls().add(negativeControls.get(1));
    
    cca.getTrios().add(ccaTrio); // add ref cca -> pair
    
    cca = studyCCARepository.save(cca);

    // Create SCC
    StudySCC scc = new StudySCC();
    StudySCCPair sccPair = new StudySCCPair();
    sccPair.setTarget(t_cohort);
    sccPair.setOutcome(o_cohort);
    sccPair.setSsc(scc);
    // associate second and third NC with this pair
    sccPair.getNegativeControls().add(negativeControls.get(1));
    sccPair.getNegativeControls().add(negativeControls.get(2));
    
    scc.getPairs().add(sccPair);
    
    scc = studySCCRepository.save(scc);
    
    Study s = new Study();
    s.setName("Test Study");
    s.setDescription("Some Desc");

    // add cohorts
    s.getCohortList().add(i_cohort);
    s.getCohortList().add(t_cohort);
    s.getCohortList().add(c_cohort);
    s.getCohortList().add(o_cohort);
    s.getCohortList().addAll(negativeControls);
      
    
    // add IR
    s.getIrAnalysisList().add(ira);
    
    // add CCA
    s.getCcaList().add(cca); 

    // add SCC
    
    s.getSccList().add(scc);

    s = studyRepository.save(s); 
  }

}