Feature: Movie list
  CRUD operations on the movie list
  mark films as pending/watched

# TODO: I am injecting the entry in the DB right now. I must implement the UI interaction
  Scenario: Create entry
    Given an empty list of films
    When the user creates a new entry with the title "The Matrix"
    Then the list should contain an entry with the title "The Matrix"

  Scenario: Show entry
    Given a list with an entry "A Beautiful Mind"
    When the user opens the entry "A Beautiful Mind"
    Then the card containing the information of "A Beautiful Mind" should be visible

  Scenario: Archive entry
    entries are not directly deleted, only archived
    Given a list with an entry "Gone with the Wind"
    When the user opens the entry "Gone with the Wind"
    When the user archives the current entry
    Then the list should not contain an entry with the title "Gone with the Wind"

  Scenario: New entries are marked as pending by default
    Given an empty list of films
    And the list is in mode "ALL"
    When the user creates a new entry with the title "Zoolander"
    And the user opens the entry "Zoolander"
    Then the entry in the details view is marked as pending

  Scenario: Mark entry as watched and filter it out
    Given a list with an entry "Mary Poppins"
    And the list is in mode "PENDING"
    When the user opens the entry "Mary Poppins"
    And the user marks the entry as watched
    And the user navigates back to the list
    Then the list should not contain an entry with the title "Mary Poppins"

  Scenario: Mark entry as pending and filter it out
    Given a list with an entry "Paprika" that is marked as watched
    And the list is in mode "WATCHED"
    When the user opens the entry "Paprika"
    When the user marks the entry as pending
    And the user navigates back to the list
    Then the list should not contain an entry with the title "Paprika"
