package org.example.tnal_youth_backend.activity.expense.mapper;

import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseResponse;
import org.example.tnal_youth_backend.activity.expense.entity.ActivityExpense;
import org.springframework.stereotype.Component;

@Component
public class ActivityExpenseMapper {

    public ActivityExpenseResponse toResponse(
            ActivityExpense expense
    ) {
        if (expense == null) {
            return null;
        }

        return ActivityExpenseResponse.builder()
                .id(expense.getId())
                .activityId(
                        expense.getActivity() != null
                                ? expense.getActivity().getId()
                                : null
                )
                .name(expense.getName())
                .description(expense.getDescription())
                .quantity(expense.getQuantity())
                .amountKhr(expense.getAmountKhr())
                .amountUsd(expense.getAmountUsd())
                .totalAmountUsd(
                        expense.getTotalAmountUsd()
                )
                .spentOn(expense.getSpentOn())
                .receiptFileId(
                        expense.getReceiptFile() != null
                                ? expense.getReceiptFile().getId()
                                : null
                )
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}