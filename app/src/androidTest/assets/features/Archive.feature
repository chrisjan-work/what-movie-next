@ArchiveFeature
Feature: Archive
  Archive and delete or restore entries

  Scenario: Archive entry
    Given a list with an entry "Gone with the Wind"
    When the user opens the entry "Gone with the Wind"
    And the user starts editing the entry
    And the user archives the current entry
    Then the entry "Gone with the Wind" is not available
    And the user navigates to the archive
    Then the entry "Gone with the Wind" is visible in the archive

  Scenario: Delete entry forever
    Given a list with an entry "The Room"
    And a list with an entry "The Disaster Artist"
    When the user archives the entry "The Room"
    And the user archives the entry "The Disaster Artist"
    And the user navigates to the archive
    And the user selects the entry "The Room" in the archive
    And the user clicks on the archive action "Delete forever"
    Then a pop-up asks for confirmation for deleting the entry
    When the user selects "Confirm" in the deletion pop-up
    Then the entry "The Room" is not available in the archive
    When the user navigates back
    Then the entry "The Room" is not available

  Scenario: Attempt to delete entry but cancel
    Given a list with an entry "The Room"
    When the user archives the entry "The Room"
    And the user navigates to the archive
    And the user selects the entry "The Room" in the archive
    And the user clicks on the archive action "Delete forever"
    Then a pop-up asks for confirmation for deleting the entry
    When the user selects "Cancel" in the deletion pop-up
    Then the entry "The Room" is visible in the archive

  Scenario: Restore entry from archive
    Given a list with an entry "Hellraiser"
    And a list with an entry "Ghost Rider"
    When the user archives the entry "Hellraiser"
    And the user archives the entry "Ghost Rider"
    And the user navigates to the archive
    And the user selects the entry "Hellraiser" in the archive
    And the user clicks on the archive action "Restore"
    Then the entry "Hellraiser" is not available in the archive
    When the user navigates back
    Then the entry "Hellraiser" is visible

  Scenario: archive should not be reachable when empty
    Given a list with an entry "Cast Away"
    Then the archive shortcut is not available

  Scenario: leaving archive automatically when emptied
    Given a list with an entry "Ghoulies Go to College"
    And a list with an entry "Some like it hot"
    When the user archives the entry "Ghoulies Go to College"
    And the user navigates to the archive
    And the user selects the entry "Ghoulies Go to College" in the archive
    And the user clicks on the archive action "Delete forever"
    Then a pop-up asks for confirmation for deleting the entry
    When the user selects "Confirm" in the deletion pop-up
    Then the list view is visible
    And the archive shortcut is not available
