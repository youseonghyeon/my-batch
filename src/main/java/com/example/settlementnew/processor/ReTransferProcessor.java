package com.example.settlementnew.processor;

import com.example.settlementnew.api.History;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.repository.TransferHistoryRepository;
import com.example.settlementnew.service.MessageBroker;
import com.example.settlementnew.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReTransferProcessor implements ItemProcessor<TransferHistory, TransferHistory> {

    private final TransferHistoryRepository transferHistoryRepository;
    private final TransferService transferService;
    private final MessageBroker messageBroker;

    @Override
    public TransferHistory process(TransferHistory transferHistory) throws Exception {
        // 재송금 한 로그는 REATTEMPT 상태로 변경
        Long id = transferHistory.getId();
        TransferHistory his = transferHistoryRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        his.setStatus(TransferStatus.REATTEMPT);

        History history = transferService.transfer(transferHistory.getToUsername(), transferHistory.getAmount());
        TransferStatus transferStatus = history.isStatus() ? TransferStatus.COMPLETED : TransferStatus.FAILED;
        if (transferStatus == TransferStatus.COMPLETED) {
            messageBroker.sendMessage(transferHistory.getToUsername() + "송금이 완료되었습니다.");
        }
        return new TransferHistory(transferStatus, history.getFrom(), history.getTo(), history.getAmount());
    }
}
