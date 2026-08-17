package org.example.tnal_youth_backend.activity.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPageResponse {

    private List<ActivityListItemResponse> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;

    /*
     * Total number of activities the viewer reaches only through an
     * ACCEPTED co-hosting invitation to another branch (not limited to the
     * current page — this is the full count across every matching
     * activity, same source as the ownBranch=false rows in `content`).
     * Only populated for a SECRETARY viewer, same as
     * ActivityListItemResponse.ownBranch; null for every other role.
     */
    private Long invitedActivityCount;
}