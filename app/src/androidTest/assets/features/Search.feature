@SearchFeature
Feature: Search Online
  when entering movie details, the user can search for a movie online

  Background: Start with an empty repo each time
    Given the online repo is empty

# TODO
#  Scenario: Search movie with multiple options
#    Given the user initiates a new entry
#    Given the online repository contains 2 entries with the title "Spaceballs"
#    When the user searches for the title "Spaceballs"
#    Then the search results view contains 2 entries with the title "Spaceballs"
#
## TODO
#  Scenario: Choose search results with multiple options
#    Given the user initiates a new entry
#    Given the online repository contains 3 entries with the title "Spaceballs"
#    When the user searches for the title "Spaceballs"
#    Then the search results view contains 3 entries with the title "Spaceballs"
#    When the user selects the search result "Spaceballs"
#    Then the card is filled with the information of "Spaceballs"

  Scenario: Search movie with single option
    Given the online repo returns an entry with title "Unique Movie"
    When the user searches for the title "Unique"
    Then the edit card title is filled with "Unique Movie"
#
#  # TODO
#  Scenario: Search movie with no results
#    Given the user initiates a new entry
#    When the user searches for the title "Non-existent Movie"
#    Then a pop-up is shown informing that no results were found
#
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
