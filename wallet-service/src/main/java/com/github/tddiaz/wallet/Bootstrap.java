package com.github.tddiaz.wallet;

import com.github.f4b6a3.tsid.TsidCreator;
import com.github.tddiaz.wallet.model.Wallet;
import com.github.tddiaz.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Bootstrap {

    private final WalletRepository walletRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        log.info("saving wallet test data on DB");

        var wallet = Wallet.initialize(TsidCreator.getTsid().toLong(), "GBP");
        walletRepository.save(wallet);

        log.info("TEST DATA: wallet id - {}, customer id - {}", wallet.getId(), wallet.getCustomerId());
    }
}
