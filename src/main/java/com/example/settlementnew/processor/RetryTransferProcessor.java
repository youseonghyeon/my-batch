package com.example.settlementnew.processor;

import com.example.settlementnew.api.History;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.repository.TransferHistoryRepository;
import com.example.settlementnew.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RetryTransferProcessor implements ItemProcessor<TransferHistory, TransferHistory> {

    private final TransferHistoryRepository transferHistoryRepository;
    private final TransferService transferService;

    @Override
    public TransferHistory process(TransferHistory transferHistory) {
        // 재송금 한 로그는 REATTEMPT 상태로 변경
        // TODO Transaction 설정을 추가적으로 해줘야 함
        Long id = transferHistory.getId();
        TransferHistory th = transferHistoryRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        th.setStatus(TransferStatus.REATTEMPT);

        History history = retryTransfer(transferHistory);

        TransferStatus transferStatus = history.isStatus() ? TransferStatus.COMPLETED : TransferStatus.FAILED;
        return new TransferHistory(transferStatus, history.getFrom(), history.getTo(), history.getAmount(), transferHistory.getDailySettlement());
    }

    private History retryTransfer(TransferHistory transferHistory) {
        return transferService.transfer(transferHistory.getToUsername(), transferHistory.getAmount());
    }
}
