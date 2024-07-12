Feature: Movie card
  Interactions with the movie details view

  Scenario: Create card
    When the user initiates a new entry
    Then An editable movie details view is open
    And the title input is focused

  Scenario: Add new card
    Given an empty list of films
    And the user initiates a new entry
    When the user enters the title "Airplane"
    And the user saves the entry
    Then the entry "Airplane" is visible

  # TODO: rethink this scenario
#  Scenario: Attempt to add duplicate card
#    Given a list with an entry "Raiders of the lost ark"
#    And the user initiates a new entry
#    When the user enters the title "Raiders of the Lost Ark"
#    Then an error message is displayed indicating that the entry already exists
#    And the save function is disabled

  Scenario: Attempt to create card with empty title
    Given the user initiates a new entry
    And the edited title is empty
    When the user saves the entry
    Then an error message is displayed indicating that the title is required

  Scenario: Attempt to leave filled card view without saving
    Given the user initiates a new entry
    When the user enters the title "Airplane"
    And the user navigates back
    Then an alert message gives the user the option to save or discard the changes

  Scenario: Saving card through alert
    Given an empty list of films
    And the user initiates a new entry
    When the user enters the title "Airplane"
    And the user navigates back
    Then an alert message gives the user the option to save or discard the changes
    When the user selects the option "Save"
    Then the entry "Airplane" is visible

  Scenario: Discarding card through alert
    Given an empty list of films
    And the user initiates a new entry
    When the user enters the title "Airplane"
    And the user navigates back
    Then an alert message gives the user the option to save or discard the changes
    When the user selects the option "Discard"
    Then the entry "Airplane" is not available

  Scenario: Dismissing dialog and continue editing
    Given an empty list of films
    And the user initiates a new entry
    When the user enters the title "Robin Hood Prince of Thieves"
    And the user navigates back
    Then an alert message gives the user the option to save or discard the changes
    When the user selects the option "Continue Editing"
    Then the edit view is visible
    And an alert messages giving the user the option to save or discard the changes is gone

  Scenario: Attempt to leave empty card view without saving
    Given a list with an entry "The One"
    And the user initiates a new entry
    And the edited title is empty
    When the user navigates back
    Then an alert messages giving the user the option to save or discard the changes is gone
    And the list view is visible

  @Journey
  Scenario: Edit existing card
    Given a list with an entry "Young Frankenstein"
    When the user opens the entry "Young Frankenstein"
    Then the card containing the information of "Young Frankenstein" should be visible
    When the user starts editing the entry
    And the user enters the title "The Big Lebowski"
    And the user saves the entry
    Then the card containing the information of "The Big Lebowski" should be visible
    When the user navigates back
    Then the entry "The Big Lebowski" is visible
    And the entry "Young Frankenstein" is not available

  # TODO: rethink this scenario
#  Scenario: Edit existing card with duplicate title
#    Given a list with an entry "Young Frankenstein"
#    And a list with an entry "The Big Lebowski"
#    When the user opens the entry "Young Frankenstein"
#    And the user starts editing the entry
#    And the user enters the title "The Big Lebowski"
#    Then an error message is displayed indicating that the entry already exists
#    And the save function is disabled

# TODO
#  Scenario: Search movie
#    Given the user initiates a new entry
#    When the user searches for the title "Spaceballs"
#    Then the search results view should contain an entry with the title "Spaceballs"
#
# TODO
#  Scenario: Choose search results
#    Given the user initiates a new entry
#    When the user searches for the title "Spaceballs"
#    When the user selects the search result "Spaceballs"
#    Then the card is filled with the information of "Spaceballs"
