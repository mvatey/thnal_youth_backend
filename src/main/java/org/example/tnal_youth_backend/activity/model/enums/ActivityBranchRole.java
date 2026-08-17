package org.example.tnal_youth_backend.activity.model.enums;

/**
 * A branch's relationship to one activity — used only by the read-only
 * "activity branches" view ({@code GET /api/activities/{activityId}/branches}),
 * which lists every branch connected to an activity (the organizer plus
 * every invited branch) tagged with its role.
 *
 * <p>This is deliberately NOT a stored column. The organizer relationship
 * already exists as {@code activities.branch_id} (a required, single FK —
 * every activity has exactly one organizer, never a list of them), and the
 * invited-branch relationship already exists as rows in
 * {@code activity_invited_branches} (which can never contain the organizer
 * itself — see {@code ActivityInvitedBranchServiceImpl
 * .validateBranchIsNotHostBranch}). Turning ORGANIZER into a real stored row
 * would mean migrating every existing activity's implicit organizer into an
 * explicit row and touching every place in the codebase that currently reads
 * {@code activities.branch_id} directly — a large, invasive change for no
 * behavioral gain. This enum instead labels the two existing sources when
 * they are read together for display purposes.
 */
public enum ActivityBranchRole {
    ORGANIZER,
    INVITED
}
