package com.postech.execution.application.messaging;

import com.postech.execution.domain.model.ExecucaoTrabalho;

public interface ExecutionEventPublisher {

    void publishExecucaoIniciada(ExecucaoTrabalho execucao);

    void publishExecucaoFinalizada(ExecucaoTrabalho execucao);
}
