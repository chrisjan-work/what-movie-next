Feature: Movie list
  CRUD operations on the movie list
  mark films as pending/watched

# TODO: I am injecting the entry in the DB right now. I must implement the UI interaction
  Scenario: Create entry
    Given an empty list of films
    When the user creates a new entry with the title "The Matrix"
    Then the list should contain an entry with the title "The Matrix"

# TODO
#  Scenario: Show entry
#    Given a list with an entry "A Beautiful Mind"
#    When the user opens the entry "A Beautiful Mind"
#    Then the card containing the information of "A Beautiful Mind" should be visible

  # TODO
#  Scenario: Delete entry
#    Given a list with an entry "Gone with the Wind"
#    When the user deletes the entry "Gone with the Wind"
#    Then the list should not contain an entry with the title "Gone with the Wind"

  # TODO
#  Scenario: Edit entry
#    Given a list with an entry "Beetlejuice"
#    When the user edits the entry "Beetlejuice" and changes its title to "Goodfellas"
#    Then the list should contain an entry with the title "Goodfellas"
#    And the list should not contain an entry with the title "Beetlejuice"

  # TODO
#  Scenario: New entries are marked as pending by default
#    Given an empty list of films
#    When the user creates a new entry with the title "Zoolander"
#    Then the list should contain an entry with the title "Zoolander"
#    And the entry "Zoolander" is marked as pending
  
  # TODO
#  Scenario: Mark entry as watched and filter it out
#    Given a list with an entry "Mary Poppins"
#    And the entry "Mary Poppins" is not marked as watched
#    When the user marks the entry "Mary Poppins" as watched
#    And the user filters out watched entries
#    Then the list should not contain an entry with the title "Mary Poppins"

  # TODO
#  Scenario: Mark entry as pending and filter it out
#    Given a list with an entry "Paprika"
#    And the entry "Paprika" is marked as watched
#    When the user marks the entry "Paprika" as pending
#    And the user filters out pending entries
#    Then the list should not contain an entry with the title "Paprika"
