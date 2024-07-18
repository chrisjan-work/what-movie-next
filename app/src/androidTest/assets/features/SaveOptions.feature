@SaveOptionsFeature
Feature: Save Options
  Test matrix for the saving paths after editing a card

  Scenario Outline: Saving cards in all combinations "<case>"
    Given a list with an entry "<title>"
    And a list with an entry "<other>"
    When the user creates or opens the entry "<toedit>"
    And the user enters the title "<newtitle>"
    And the user saves via "<saveMethod>"
    And the title config dialog is answered with "<overwriteMethod>"
    Then the card view shows "<newCard>"
    And the list contains the entries "<titleList>"

    # legend:
    # "cr" or "create" = create a new movie with the add button
    # "up" or "update" = edit an existing movie
    # "exp" or "explicit" = save with explicit save button in edit view
    # "acc" or "accept" = leave edit view, press "save" in reminder pop-up
    # "decline" = leave edit view, press "discard" in reminder pop-up
    # "dismiss" = press "continue editing" in reminder pop-up to stay in edit view
    # "dup" or "duplicate" = the new title is duplicate of the existing movie
    # "ovr" or "overwrite" = overwrite the existing movie with the new data
    # "del" or "discard" = discard the new data, leaving it unsaved
    # "ign" or "ignore" = close the overwrite dialog, staying in the edit view

    Examples:
      | case            | title  | other  | toedit | newtitle | saveMethod  | overwriteMethod | newCard | titleList |

      | create explicit | Movie1 | Movie2 | -      | Movie3   | Explicit    | -               | Movie3  | Movie1,Movie2,Movie3 |
      | update explicit | Movie1 | Movie2 | Movie1 | Movie3   | Explicit    | -               | Movie3  | Movie2,Movie3 |

      | cr-accept       | Movie1 | Movie2 | -      | Movie3   | AcceptSave  | -               | Movie3  | Movie1,Movie2,Movie3 |
      | up-accept       | Movie1 | Movie2 | Movie1 | Movie3   | AcceptSave  | -               | Movie3  | Movie2,Movie3 |
      | cr-decline      | Movie1 | Movie2 | -      | Movie3   | RejectSave  | -               | -       | Movie1,Movie2 |
      | up-decline      | Movie1 | Movie2 | Movie1 | Movie3   | RejectSave  | -               | Movie1  | Movie1,Movie2 |
      | cr-dismiss      | Movie1 | Movie2 | -      | Movie3   | DismissSave | -               | -       | - |
      | up-dismiss      | Movie1 | Movie2 | Movie1 | Movie3   | DismissSave | -               | -       | - |

      | cr-dup-exp-ovr  | Movie1 | Movie2 | -      | Movie1   | Explicit    | Overwrite       | Movie1  | Movie1,Movie2 |
      | cr-dup-exp-del  | Movie1 | Movie2 | -      | Movie1   | Explicit    | Discard         | -       | Movie1,Movie2 |
      | cr-dup-exp-ign  | Movie1 | Movie2 | -      | Movie1   | Explicit    | Ignore          | -       | - |
      | up-dup-exp-ovr  | Movie1 | Movie2 | Movie2 | Movie1   | Explicit    | Overwrite       | Movie1  | Movie1 |
      | up-dup-exp-del  | Movie1 | Movie2 | Movie2 | Movie1   | Explicit    | Discard         | Movie2  | Movie1,Movie2 |
      | up-dup-exp-ign  | Movie1 | Movie2 | Movie2 | Movie1   | Explicit    | Ignore          | -       | - |

      | cr-dup-acc-ovr  | Movie1 | Movie2 | -      | Movie1   | AcceptSave  | Overwrite       | Movie1  | Movie1,Movie2 |
      | cr-dup-acc-del  | Movie1 | Movie2 | -      | Movie1   | AcceptSave  | Discard         | -       | Movie1,Movie2 |
      | cr-dup-acc-ign  | Movie1 | Movie2 | -      | Movie1   | AcceptSave  | Ignore          | -       | - |
      | up-dup-acc-ovr  | Movie1 | Movie2 | Movie2 | Movie1   | AcceptSave  | Overwrite       | Movie1  | Movie1 |
      | up-dup-acc-del  | Movie1 | Movie2 | Movie2 | Movie1   | AcceptSave  | Discard         | Movie2  | Movie1,Movie2 |
      | up-dup-acc-ign  | Movie1 | Movie2 | Movie2 | Movie1   | AcceptSave  | Ignore          | -       | - |

      | cr-dup-decline  | Movie1 | Movie2 | -      | Movie1   | RejectSave  | -               | -       | Movie1,Movie2 |
      | up-dup-decline  | Movie1 | Movie2 | Movie2 | Movie1   | RejectSave  | -               | Movie2  | Movie1,Movie2 |
      | cr-dup-dismiss  | Movie1 | Movie2 | -      | Movie1   | DismissSave | -               | -       | - |
      | up-dup-dismiss  | Movie1 | Movie2 | Movie2 | Movie1   | DismissSave | -               | -       | - |