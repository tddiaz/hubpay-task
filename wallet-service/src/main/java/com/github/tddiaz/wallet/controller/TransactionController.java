package com.github.tddiaz.wallet.controller;

import com.github.tddiaz.wallet.controller.dto.GetTransactionsRequestDto;
import com.github.tddiaz.wallet.controller.dto.GetTransactionsResponseDto;
import com.github.tddiaz.wallet.controller.dto.constraints.ValidEnum;
import com.github.tddiaz.wallet.service.GetTransactionsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final GetTransactionsUseCase getTransactionsUseCase;

    @GetMapping("/all")
    public ResponseEntity<GetTransactionsResponseDto> getTransactions(@ModelAttribute @Valid GetTransactionsRequestDto request) {
        return ResponseEntity.ok(getTransactionsUseCase.execute(request));
    }

}
