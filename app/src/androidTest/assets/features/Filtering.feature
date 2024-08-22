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

@FilteringFeature
Feature: Filtering
  mark films as pending/watched

  Scenario: New entries are marked as pending by default
    Given an empty list of films
    And the user clicks on Arrange and Filter
    And the list is in mode "All movies"
    And the user presses the back button
    When the user creates a new entry with the title "Zoolander"
    And the user opens the entry "Zoolander"
    Then the entry in the card view is marked as pending

  Scenario: Mark entry as watched and filter it out
    Given a list with an entry "Mary Poppins"
    And the user clicks on Arrange and Filter
    And the list is in mode "To watch"
    When the user opens the entry "Mary Poppins"
    And the user marks the entry as watched
    And the user presses the back button
    Then the entry "Mary Poppins" is not available

  Scenario: Mark entry as pending and filter it out
    Given a list with an entry "Paprika" that is marked as watched
    And the user clicks on Arrange and Filter
    And the list is in mode "Seen"
    When the user opens the entry "Paprika"
    When the user marks the entry as pending
    And the user presses the back button
    Then the entry "Paprika" is not available

  Scenario: filter by minimum runtime
    Given a list with an entry "Sharknado"
    And the db entry "Sharknado" has "runtime" set as "116"
    Then the entry "Sharknado" is visible
    When the user clicks on Arrange and Filter
    And the user clicks on "runtime"
    And the user enters "120" in the input "at least"
    And the user clicks on "Update"
    Then the entry "Sharknado" is not available

  Scenario: filter by maximum runtime
    Given a list with an entry "Sharknado"
    And the db entry "Sharknado" has "runtime" set as "116"
    Then the entry "Sharknado" is visible
    When the user clicks on Arrange and Filter
    And the user clicks on "runtime"
    And the user enters "100" in the input "at most"
    And the user clicks on "Update"
    Then the entry "Sharknado" is not available

  Scenario: filter by some runtimes
    Given a list with an entry "Short"
    And the db entry "Short" has "runtime" set as "100"
    And a list with an entry "Medium"
    And the db entry "Medium" has "runtime" set as "120"
    And a list with an entry "Large"
    And the db entry "Large" has "runtime" set as "140"
    Then the entry "Short" is visible
    And the entry "Medium" is visible
    And the entry "Large" is visible
    When the user clicks on Arrange and Filter
    And the user clicks on "runtime"
    And the user enters "110" in the input "at least"
    And the user enters "130" in the input "at most"
    And the user clicks on "Update"
    Then the entry "Short" is not available
    Then the entry "Medium" is visible
    Then the entry "Large" is not available
