-- A position mapped to VIEWER can now also specify which branch-scoped
-- level (BRANCH_LEADER or SECRETARY) the resulting member-linked viewer
-- sees their own branch at -- e.g. two distinct positions, "Viewer (as
-- Branch Leader)" and "Viewer (as Secretary)", both mapped_role=VIEWER
-- but with different mapped_viewer_scope. NULL for every other
-- mapped_role. See Position#mappedViewerScope /
-- MemberPasswordServiceImpl's VIEWER promotion branch.
ALTER TABLE positions
    ADD COLUMN IF NOT EXISTS mapped_viewer_scope VARCHAR(30);

ALTER TABLE positions
    ADD CONSTRAINT chk_positions_mapped_viewer_scope
    CHECK (mapped_viewer_scope IS NULL OR mapped_viewer_scope IN ('BRANCH_LEADER', 'SECRETARY'));
