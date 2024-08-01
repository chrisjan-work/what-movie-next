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

@MovieCardFeature
Feature: Movie card
  Displaying movie information in a card

  Scenario: Show card from database
    # fake original title for test purposes
    Given a list with an entry "Time Bandits"
    And the db entry "Time Bandits" has "original title" set as "The Time Bandits"
    And the db entry "Time Bandits" has "year" set as "1981"
    And the db entry "Time Bandits" has "runtime" set as "116"
    And the db entry "Time Bandits" has "genres" set as "Adventure,Comedy,Family,Fantasy"
    And the db entry "Time Bandits" has "tagline" set as "they stole history"
    And the db entry "Time Bandits" has "plot" set as "Time traveling dwarfs meet Sean Connery"
    When the user opens the entry "Time Bandits"
    Then the card containing the information of "Time Bandits" should be visible
    And the card contains the text "The Time Bandits"
    And the card contains the text "1981"
    And the card contains the text "1h 56min"
    And the card contains the text "Adventure / Comedy / Family / Fantasy"
    And the card contains the text "they stole history"
    And the card contains the text "Time traveling dwarfs meet Sean Connery"

  Scenario: Pull card from backend and show in card view
    # fake original title for test purposes
    # genres need mappings, which would make this scenario more complex and is covered by other tests
    Given the online repo returns an entry with title "The Fisher King"
    And the extended entry "The Fisher King" has "original title" set as "King of Fishing"
    And the extended entry "The Fisher King" has "year" set as "1991"
    And the extended entry "The Fisher King" has "runtime" set as "138"
    And the extended entry "The Fisher King" has "tagline" set as "A Modern Day Retelling of the Arthurian Legend"
    And the extended entry "The Fisher King" has "plot" set as "Search for the Holy Grail"
    When the user searches for the title "Fisher"
    And the user saves the entry
    And the user navigates to the list
    And the user opens the entry "The Fisher King"
    Then the card containing the information of "The Fisher King" should be visible
    And the card contains the text "King of Fishing"
    And the card contains the text "1991"
    And the card contains the text "2h 18min"
    And the card contains the text "A Modern Day Retelling of the Arthurian Legend"
    And the card contains the text "Search for the Holy Grail"

