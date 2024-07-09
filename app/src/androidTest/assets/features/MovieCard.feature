Feature: Movie card
  Interactions with the movie details view

  Scenario: Create card
    When the user initiates a new entry
    Then An editable movie details view is open
    And the title input is focused

