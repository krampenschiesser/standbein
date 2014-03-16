package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.cdi.WorkflowSpecificLiteral;
import de.ks.workflow.step.EditStep;
import de.ks.workflow.step.PersistStep;
import de.ks.workflow.step.TableSelectionStep;
import de.ks.workflow.step.WorkflowStepConfig;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.scene.layout.BorderPane;

@WorkflowScoped
public class TestWorkflow extends Workflow<SimpleWorkflowModel, BorderPane, FullWorkflowView> {
  private SimpleWorkflowModel model = new SimpleWorkflowModel();

  public static WorkflowSpecificLiteral getLiteral() {
    return new WorkflowSpecificLiteral(TestWorkflow.class);
  }

  @Override
  public SimpleWorkflowModel getModel() {
    return model;
  }

  @Override
  public Class<SimpleWorkflowModel> getModelClass() {
    return SimpleWorkflowModel.class;
  }

  @Override
  protected void configureSteps() {
    WorkflowStepConfig root = cfg.startWith(EditStep.class);
    WorkflowStepConfig persistStep = root.next(PersistStep.class);
    persistStep.error(root);

    WorkflowStepConfig selection = persistStep.next(TableSelectionStep.class);
    WorkflowStepConfig background = selection.next(PersistStep.class);

    background.branch("first", EditStep.class).next(PersistStep.class).restartWithNext();
    background.branch("second", EditStep.class).next(PersistStep.class).restartWithNext();
  }

}