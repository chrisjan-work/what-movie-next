@SearchFeature
Feature: Search Online
  when entering movie details, the user can search for a movie online

  Background: Start with an empty repo each time
    Given the online repo is empty

  Scenario: Search movie with multiple options
    Given the online repo returns an entry with title "Star Trek The Movie"
    And the online repo returns an entry with title "Star Wars: A New Hope"
    When the user searches for the title "Star"
    Then the search results contains an entry with title "Star Trek The Movie"
    And the search results contains an entry with title "Star Wars: A New Hope"

  Scenario: Choose search results with multiple options
    Given the online repo returns an entry with title "The City of Lost Children"
    And the online repo returns an entry with title "Dark City"
    And the online repo returns an entry with title "City of Angels"
    When the user searches for the title "City"
    Then the search results contains an entry with title "Dark City"
    When the user selects the search result "Dark City"
    Then the edit card title is filled with "Dark City"
    And the search results are not visible

  Scenario: Search movie with single option
    Given the online repo returns an entry with title "Unique Movie"
    When the user searches for the title "Unique"
    Then the edit card title is filled with "Unique Movie"

  Scenario: Search movie with no results
    When the user searches for the title "Non-existent Movie"
    Then a pop-up is shown informing that no results were found

  Scenario: Search movie with error
    Given the online repo throws an error
    When the user searches for the title "Failure"
    Then a pop-up is shown informing that an error occurred

  Scenario: When search is launched, focus is cleared
    Given the online repo returns an entry with title "Close Encounters of the Third Kind"
    Given the user initiates a new entry
    When the user enters the title "Close"
    Then the title input is focused
    When the user clicks on the find button
    Then the title input is not focused

  Scenario: Back button closes search results
    Given the online repo returns an entry with title "The Day After Tomorrow"
    And the online repo returns an entry with title "Edge of Tomorrow"
    When the user searches for the title "Tomorrow"
    Then the search results contains an entry with title "The Day After Tomorrow"
    When the user navigates back
    Then the edit card title is filled with "Tomorrow"
    And the search results are not visible

#  # TODO
#  Scenario: App is offline
#    Given the user initiates a new entry
#    When the device is offline
#    Then the app indicates that it is offline
#    When the user attempts to search for a movie
#    Then a dialog is shown informing that search is disabled while offline
#
#  # TODO
#  Scenario: Switching on- and offline modes
#    Given the user initiates a new entry
#    When the device is offline
#    Then the app indicates that it is offline
#    When the connection is switched on
#    Then the search button is enabled
#    When the connection is switched off
#    Then the app indicates that it is offline
