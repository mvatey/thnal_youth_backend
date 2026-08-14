package org.example.tnal_youth_backend.activity.model.enums;

public enum ParticipantRegistrationSource {
    MANUAL,
    HOST_BRANCH,
    INVITED_BRANCH,
    SELF_REGISTERED,

    /*
     * Recorded after the fact by staff marking a host-branch member's
     * real-world attendance without a prior formal invite/registration
     * step — e.g. someone who showed up and joined an activity even
     * though they weren't on the invite list. Distinguishes "invited
     * beforehand" from "not invited but still attended" in the
     * participants page's invitation-status column.
     */
    WALK_IN
}