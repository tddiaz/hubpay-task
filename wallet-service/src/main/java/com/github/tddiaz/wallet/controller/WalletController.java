package com.github.tddiaz.wallet.controller;

import com.github.tddiaz.wallet.controller.dto.*;
import com.github.tddiaz.wallet.service.CreateDepositOrderUseCase;
import com.github.tddiaz.wallet.service.CreateWithdrawOrderUseCase;
import com.github.tddiaz.wallet.service.GetWalletDetailsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final CreateDepositOrderUseCase createDepositOrderUseCase;

    private final CreateWithdrawOrderUseCase createWithdrawOrderUseCase;

    private final GetWalletDetailsUseCase getWalletDetailsUseCase;

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponseDto> deposit(@RequestBody @Valid DepositRequestDto request) {
        return ResponseEntity.ok(createDepositOrderUseCase.execute(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponseDto> withdraw(@RequestBody @Valid WithdrawRequestDto request) {
        return ResponseEntity.ok(createWithdrawOrderUseCase.execute(request));
    }

    @GetMapping("/{walletId}/customer/{customerId}")
    public ResponseEntity<GetWalletDetailsResponseDto> getWalletDetails(
        @PathVariable("walletId") Long walletId,
        @PathVariable("customerId") Long customerId
    ) {
        return ResponseEntity.ok(getWalletDetailsUseCase.execute(walletId, customerId));
    }

}
