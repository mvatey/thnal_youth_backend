package org.example.tnal_youth_backend.activity.expense.service;

import org.example.tnal_youth_backend.activity.expense.dto.request.CreateActivityExpenseRequest;
import org.example.tnal_youth_backend.activity.expense.dto.request.UpdateActivityExpenseRequest;
import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseResponse;
import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseSummaryResponse;

import java.util.List;

public interface ActivityExpenseService {

    ActivityExpenseResponse createExpense(
            Long activityId,
            CreateActivityExpenseRequest request,
            Long currentUserId
    );

    List<ActivityExpenseResponse> getExpenses(
            Long activityId
    );

    ActivityExpenseResponse getExpense(
            Long activityId,
            Long expenseId
    );

    ActivityExpenseResponse updateExpense(
            Long activityId,
            Long expenseId,
            UpdateActivityExpenseRequest request,
            Long currentUserId
    );

    void deleteExpense(
            Long activityId,
            Long expenseId,
            Long currentUserId
    );

    ActivityExpenseSummaryResponse getSummary(
            Long activityId
    );
}