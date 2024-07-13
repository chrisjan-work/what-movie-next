Feature: Movie list
  CRUD operations on the movie list
  mark films as pending/watched

  Scenario: Create entry
    Given an empty list of films
    When the user creates a new entry with the title "The Matrix"
    Then the entry "The Matrix" is visible

  Scenario: Show entry
    Given a list with an entry "A Beautiful Mind"
    When the user opens the entry "A Beautiful Mind"
    Then the card containing the information of "A Beautiful Mind" should be visible

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
    Then the entry "Mary Poppins" is not available

  Scenario: Mark entry as pending and filter it out
    Given a list with an entry "Paprika" that is marked as watched
    And the list is in mode "WATCHED"
    When the user opens the entry "Paprika"
    When the user marks the entry as pending
    And the user navigates back to the list
    Then the entry "Paprika" is not available

  Scenario: The Movie list is scrollable
    Given a list with 100 entries, titled "movieN" where "N" is the index
    Then the entry "movie100" is not available
    When the user scrolls down to "movie100"
    Then the entry "movie100" is visible
