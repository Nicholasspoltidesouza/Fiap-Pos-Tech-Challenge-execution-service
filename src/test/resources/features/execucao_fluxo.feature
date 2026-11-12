Feature: Workshop execution happy path
  As a workshop floor operator
  I want jobs to move from queue to diagnosis, repair and completion
  So that the OS Service saga can continue after production finishes

  Scenario: Complete execution lifecycle and allow compensation
    Given a new service order ready for execution
    When the execution start command is processed
    Then the job should be in queue with status EM_FILA
    And an ExecucaoIniciada event should have been published
    When the operator starts diagnosis
    Then the job status should be EM_DIAGNOSTICO
    When the operator starts repair
    Then the job status should be EM_REPARO
    When the operator finishes the job
    Then the job status should be FINALIZADA
    And an ExecucaoFinalizada event should have been published

  Scenario: Compensate cancels an active job
    Given a new service order ready for execution
    When the execution start command is processed
    And the compensation command is processed
    Then the job status should be CANCELADA
