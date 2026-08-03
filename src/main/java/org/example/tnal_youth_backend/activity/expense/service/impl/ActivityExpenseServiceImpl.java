package org.example.tnal_youth_backend.activity.expense.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.expense.dto.request.CreateActivityExpenseRequest;
import org.example.tnal_youth_backend.activity.expense.dto.request.UpdateActivityExpenseRequest;
import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseResponse;
import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseSummaryResponse;
import org.example.tnal_youth_backend.activity.expense.entity.ActivityExpense;
import org.example.tnal_youth_backend.activity.expense.mapper.ActivityExpenseMapper;
import org.example.tnal_youth_backend.activity.expense.repository.ActivityExpenseRepository;
import org.example.tnal_youth_backend.activity.expense.service.ActivityExpenseService;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.exchangerate.entity.ExchangeRate;
import org.example.tnal_youth_backend.exchangerate.repository.ExchangeRateRepository;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ActivityExpenseServiceImpl
        implements ActivityExpenseService {

    private static final String USD = "USD";
    private static final String KHR = "KHR";

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

    private final ActivityExpenseRepository expenseRepository;
    private final ActivityRepository activityRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final ActivityExpenseMapper expenseMapper;

    @Override
    @Transactional
    public ActivityExpenseResponse createExpense(
            Long activityId,
            CreateActivityExpenseRequest request,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        validateActivityCanBeModified(activity);

        User recordedBy = findUser(currentUserId);

        BigDecimal amountKhr =
                normaliseAmount(request.getAmountKhr());

        BigDecimal amountUsd =
                normaliseAmount(request.getAmountUsd());

        validateAmounts(amountKhr, amountUsd);

        FileEntity receiptFile =
                findReceiptFile(request.getReceiptFileId());

        ExchangeCalculation calculation =
                calculateExchangeValues(
                        amountKhr,
                        amountUsd,
                        request.getSpentOn()
                );

        ActivityExpense expense =
                ActivityExpense.builder()
                        .activity(activity)
                        .name(normaliseRequired(
                                request.getName(),
                                "Expense name"
                        ))
                        .description(
                                normaliseOptional(
                                        request.getDescription()
                                )
                        )
                        .quantity(request.getQuantity())
                        .amountKhr(amountKhr)
                        .amountUsd(amountUsd)
                        .exchangeRate(
                                calculation.exchangeRate()
                        )
                        .exchangeRateValue(
                                calculation.exchangeRateValue()
                        )
                        .convertedKhrToUsd(
                                calculation.convertedKhrToUsd()
                        )
                        .totalAmountUsd(
                                calculation.totalAmountUsd()
                        )
                        .spentOn(request.getSpentOn())
                        .receiptFile(receiptFile)
                        .recordedBy(recordedBy)
                        .build();

        ActivityExpense savedExpense =
                expenseRepository.saveAndFlush(expense);

        return expenseMapper.toResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityExpenseResponse> getExpenses(
            Long activityId
    ) {
        findActivity(activityId);

        return expenseRepository
                .findAllByActivity_IdOrderByCreatedAtAsc(
                        activityId
                )
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityExpenseResponse getExpense(
            Long activityId,
            Long expenseId
    ) {
        findActivity(activityId);

        return expenseMapper.toResponse(
                findExpense(activityId, expenseId)
        );
    }

    @Override
    @Transactional
    public ActivityExpenseResponse updateExpense(
            Long activityId,
            Long expenseId,
            UpdateActivityExpenseRequest request,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        validateActivityCanBeModified(activity);
        findUser(currentUserId);

        ActivityExpense expense =
                findExpense(activityId, expenseId);

        BigDecimal amountKhr =
                normaliseAmount(request.getAmountKhr());

        BigDecimal amountUsd =
                normaliseAmount(request.getAmountUsd());

        validateAmounts(amountKhr, amountUsd);

        FileEntity receiptFile =
                findReceiptFile(request.getReceiptFileId());

        ExchangeCalculation calculation =
                calculateExchangeValues(
                        amountKhr,
                        amountUsd,
                        request.getSpentOn()
                );

        expense.setName(
                normaliseRequired(
                        request.getName(),
                        "Expense name"
                )
        );

        expense.setDescription(
                normaliseOptional(
                        request.getDescription()
                )
        );

        expense.setQuantity(
                request.getQuantity()
        );

        expense.setAmountKhr(amountKhr);
        expense.setAmountUsd(amountUsd);

        expense.setExchangeRate(
                calculation.exchangeRate()
        );

        expense.setExchangeRateValue(
                calculation.exchangeRateValue()
        );

        expense.setConvertedKhrToUsd(
                calculation.convertedKhrToUsd()
        );

        expense.setTotalAmountUsd(
                calculation.totalAmountUsd()
        );

        expense.setSpentOn(request.getSpentOn());
        expense.setReceiptFile(receiptFile);

        ActivityExpense savedExpense =
                expenseRepository.saveAndFlush(expense);

        return expenseMapper.toResponse(savedExpense);
    }

    @Override
    @Transactional
    public void deleteExpense(
            Long activityId,
            Long expenseId,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        validateActivityCanBeModified(activity);
        findUser(currentUserId);

        ActivityExpense expense =
                findExpense(activityId, expenseId);

        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityExpenseSummaryResponse getSummary(
            Long activityId
    ) {
        findActivity(activityId);

        List<ActivityExpense> expenses =
                expenseRepository
                        .findAllByActivity_IdOrderByCreatedAtAsc(
                                activityId
                        );

        BigDecimal totalKhr = ZERO;
        BigDecimal totalUsd = ZERO;
        BigDecimal overallTotalUsd = ZERO;

        for (ActivityExpense expense : expenses) {
            totalKhr = totalKhr.add(
                    safeAmount(expense.getAmountKhr())
            );

            totalUsd = totalUsd.add(
                    safeAmount(expense.getAmountUsd())
            );

            overallTotalUsd =
                    overallTotalUsd.add(
                            safeAmount(
                                    expense.getTotalAmountUsd()
                            )
                    );
        }

        return ActivityExpenseSummaryResponse.builder()
                .activityId(activityId)
                .totalRecords(expenses.size())
                .totalKhr(
                        totalKhr.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                )
                .totalUsd(
                        totalUsd.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                )
                .overallTotalUsd(
                        overallTotalUsd.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                )
                .build();
    }

    private Activity findActivity(
            Long activityId
    ) {
        if (activityId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID is required"
            );
        }

        return activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Activity not found with ID: "
                                        + activityId
                        )
                );
    }

    private ActivityExpense findExpense(
            Long activityId,
            Long expenseId
    ) {
        if (expenseId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expense ID is required"
            );
        }

        return expenseRepository
                .findByIdAndActivity_Id(
                        expenseId,
                        activityId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Expense was not found for this activity"
                        )
                );
    }

    private User findUser(
            Long currentUserId
    ) {
        if (currentUserId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        return userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );
    }

    private FileEntity findReceiptFile(
            Long receiptFileId
    ) {
        if (receiptFileId == null) {
            return null;
        }

        return fileRepository.findById(receiptFileId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Receipt file was not found with ID: "
                                        + receiptFileId
                        )
                );
    }

    private ExchangeCalculation calculateExchangeValues(
            BigDecimal amountKhr,
            BigDecimal amountUsd,
            LocalDate spentOn
    ) {
        if (amountKhr.compareTo(BigDecimal.ZERO) == 0) {
            return new ExchangeCalculation(
                    null,
                    null,
                    ZERO,
                    amountUsd.setScale(
                            2,
                            RoundingMode.HALF_UP
                    )
            );
        }

        ExchangeRate exchangeRate =
                findExchangeRateForDate(spentOn);

        BigDecimal rateValue =
                exchangeRate.getRate();

        if (rateValue == null
                || rateValue.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exchange rate must be greater than zero"
            );
        }

        BigDecimal convertedKhrToUsd =
                amountKhr.divide(
                        rateValue,
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalAmountUsd =
                amountUsd.add(convertedKhrToUsd)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new ExchangeCalculation(
                exchangeRate,
                rateValue,
                convertedKhrToUsd,
                totalAmountUsd
        );
    }

    private ExchangeRate findExchangeRateForDate(
            LocalDate date
    ) {
        if (date == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Spent date is required"
            );
        }

        return exchangeRateRepository
                .findFirstByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(
                        USD,
                        KHR,
                        date,
                        date
                )
                .or(() ->
                        exchangeRateRepository
                                .findFirstByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(
                                        USD,
                                        KHR,
                                        date
                                )
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No USD to KHR exchange rate exists "
                                        + "for date: "
                                        + date
                        )
                );
    }

    private void validateAmounts(
            BigDecimal amountKhr,
            BigDecimal amountUsd
    ) {
        if (amountKhr.compareTo(BigDecimal.ZERO) < 0
                || amountUsd.compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expense amounts cannot be negative"
            );
        }

        if (amountKhr.compareTo(BigDecimal.ZERO) == 0
                && amountUsd.compareTo(BigDecimal.ZERO) == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one expense amount must be greater than zero"
            );
        }
    }

    private void validateActivityCanBeModified(
            Activity activity
    ) {
        if (activity.getStatus() == null
                || activity.getStatus().getCode() == null) {
            return;
        }

        String statusCode =
                activity.getStatus()
                        .getCode()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if ("COMPLETED".equals(statusCode)
                || "CANCELLED".equals(statusCode)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Expenses cannot be modified for a "
                            + "completed or cancelled activity"
            );
        }
    }

    private BigDecimal normaliseAmount(
            BigDecimal amount
    ) {
        if (amount == null) {
            return ZERO;
        }

        return amount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal safeAmount(
            BigDecimal amount
    ) {
        return amount != null
                ? amount
                : ZERO;
    }

    private String normaliseRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String normaliseOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record ExchangeCalculation(
            ExchangeRate exchangeRate,
            BigDecimal exchangeRateValue,
            BigDecimal convertedKhrToUsd,
            BigDecimal totalAmountUsd
    ) {
    }
}