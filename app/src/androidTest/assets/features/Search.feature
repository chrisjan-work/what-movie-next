# This file is part of What Movie Next.
#
# Copyright (C) 2024 Christiaan Janssen
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

@SearchFeature
Feature: Search Online
  when entering movie details, the user can search for a movie online

  Background: Start with an empty repo each time
    Given the online repo is empty

  Scenario: Search movie with single option
    Given the online repo returns an entry with title "Unique Movie"
    When the user searches for the title "Unique"
    Then the edit card title is filled with "Unique Movie"

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
    And the online repo returns details for an entry with title "Dark City"
    When the user searches for the title "City"
    Then the search results contains an entry with title "Dark City"
    When the user selects the search result "Dark City"
    Then the edit card title is filled with "Dark City"
    And the search results are not visible

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

  Scenario: search results contain data about the movie
  Given the online repo returns an entry with title "Man of Steel" from "2013" and poster "image.png"
  Given the online repo returns an entry with title "Batman vs Superman" from "2016" and poster "image.png"
  When the user searches for the title "Man"
  Then the search results contains an entry with title "Man of Steel" and year "2013"

  Scenario: search results contain genre
    Given the configuration contains the genre "Comedy" with id "100"
    And the configuration contains the genre "Drama" with id "200"
    Given the online repo returns an entry with title "Dream Scenario" and genre id "100"
    Given the online repo returns an entry with title "Requiem for a Dream" and genre id "200"
    When the user searches for the title "Dream"
    Then the search results contains an entry with title "Dream Scenario" and genre "Comedy"

  # TODO populate edit card with details

  # TODO
  # Feature: manage genres

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
