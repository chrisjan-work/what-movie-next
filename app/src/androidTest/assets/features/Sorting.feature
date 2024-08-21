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

@SortingFeature
Feature: Sorting movie list

  Scenario: Open menu
    When the user clicks on Arrange
    Then the sorting menu is visible

  Scenario: Close menu
    Given the user clicks on Arrange
    When the user presses the back button
    Then the sorting menu is not visible

  Scenario: Sort by title ascending
    Given a list with an entry "Monkey Man"
    And a list with an entry "Zatoichi"
    And a list with an entry "Anatomy"
    When the user clicks on Arrange
    And the user sorts by "title"
    Then the list contains an entry "Anatomy" in position "0"
    And the list contains an entry "Monkey Man" in position "1"
    And the list contains an entry "Zatoichi" in position "2"

  Scenario: Sort by title descending
    Given a list with an entry "Monkey Man"
    And a list with an entry "Zatoichi"
    And a list with an entry "Anatomy"
    When the user clicks on Arrange
    # click twice for inverting the sorting
    And the user taps on sort by "title" "2" times
    Then the list contains an entry "Anatomy" in position "2"
    And the list contains an entry "Monkey Man" in position "1"
    And the list contains an entry "Zatoichi" in position "0"

  Scenario: Sort by creation time, up and down
    Given a list with an entry "Monkey Man"
    And a list with an entry "Zatoichi"
    And a list with an entry "Anatomy"
    And the db entry "Monkey Man" has "creation time" set as "3000"
    And the db entry "Zatoichi" has "creation time" set as "500"
    And the db entry "Anatomy" has "creation time" set as "1500"
    When the user clicks on Arrange
    And the list contains an entry "Zatoichi" in position "0"
    Then the list contains an entry "Anatomy" in position "1"
    And the list contains an entry "Monkey Man" in position "2"
    And the user sorts by "date added"
    And the list contains an entry "Monkey Man" in position "0"
    Then the list contains an entry "Anatomy" in position "1"
    And the list contains an entry "Zatoichi" in position "2"

  Scenario Outline: Sort by <criteria>, up and down
    Given a list with an entry "Second"
    And the db entry "Second" has "<data>" set as "<second>"
    And a list with an entry "Third"
    And the db entry "Third" has "<data>" set as "<third>"
    And a list with an entry "First"
    And the db entry "First" has "<data>" set as "<first>"
    When the user clicks on Arrange
    And the user sorts by "<criteria>"
    Then the list contains an entry "First" in position "0"
    And the list contains an entry "Second" in position "1"
    And the list contains an entry "Third" in position "2"
    And the user sorts by "<criteria>"
    And the list contains an entry "Third" in position "0"
    And the list contains an entry "Second" in position "1"
    Then the list contains an entry "First" in position "2"

    Examples:
      | criteria | data | first | second | third |
      | year     | year | 1970  | 1985   | 2003  |
      | genre | genres | Action | Comedy | Horror |
      | runtime | runtime | 90 | 120 | 125 |
      | director | directors | Aaron Sorkin | Michael Bay | Terrence Malick |
      | avg. score | ratings | 10,10 | 20,30 | 50,-1 |
    # Relying on unit tests for covering random, because it is hard to test otherwise, due to randomness