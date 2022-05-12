# WordList
An app for your favorite words. Check it out on the Google Play Store [here](https://play.google.com/store/apps/details?id=imad.syed.wordlist)

## Documentation

### Basic User Actions

#### Adding Word Entries
- Entries can be added by tapping the Add button in the bottom right hand corner. This will bring up an empty Word Dialog box

#### Deleting Word Entries
- Entries can be deleted by swipping left on the desired entry. The Undo button will appear above the Add Button, allowing the user to undo deletion of entry, if they so wish

#### Editing Word Entries
- Entries can be edited by swapping right on the desired entry. This will bring up a nonempty Word Dialog box

#### Searching Word Entries
- Tapping the Search icon will allow the user to enter text. Entries with a name and/or meaning containing the text will appear on the screen
- Tapping the 'x' on the far right will close the search view, and show all word entries

### Component Details

#### The Word Dialog Box
- There are three textfields, each for name, type and meaning of the entry.
  - When adding words, these will be empty. When editing words, these will be populated with the name, type and meaning of the Word Entry being edited.
- There is also a button for providing official definitions.
  - Tapping the button fetches definitions for the text in the name field, and displays them in a list
  - Definitions are fetched from [Free Dictionary API](https://dictionaryapi.dev/)
  - Tapping on a definition will paste its text into the meaning field
