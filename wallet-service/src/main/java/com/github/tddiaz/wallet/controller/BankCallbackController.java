package com.github.tddiaz.wallet.controller;

import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto;
import com.github.tddiaz.wallet.service.SettleTransactionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/external/banking")
@RequiredArgsConstructor
public class BankCallbackController {

    private final SettleTransactionUseCase settleTransactionUseCase;

    @PostMapping("/notify-transfer-status")
    public ResponseEntity<Void> notifyTransferStatus(@RequestBody @Valid BankTransactionStatusRequestDto request) {
        settleTransactionUseCase.execute(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
