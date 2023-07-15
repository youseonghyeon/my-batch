package com.example.settlementnew.processor;

import com.example.settlementnew.api.History;
import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.service.MessageBroker;
import com.example.settlementnew.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferProcessor implements ItemProcessor<DailySettlement, TransferHistory> {
    private final TransferService transferService;
    private final MessageBroker messageBroker;

    @Override
    public TransferHistory process(DailySettlement dailySettlement) throws Exception {
        History history = transferService.transfer(dailySettlement.getUsername(), dailySettlement.getTotalPrice());
        TransferStatus transferStatus = history.isStatus() ? TransferStatus.COMPLETED : TransferStatus.FAILED;

//        if (transferStatus == TransferStatus.COMPLETED) {
//            messageBroker.sendMessage(dailySettlement.getUsername() + "송금이 완료되었습니다.");
//        }
        return new TransferHistory(transferStatus, history.getFrom(), history.getTo(), history.getAmount(), dailySettlement);
    }
}
