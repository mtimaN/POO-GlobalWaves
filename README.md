# Proiect GlobalWaves  - Etapa 3

<div align="center"><img src="https://tenor.com/view/listening-to-music-spongebob-gif-8009182.gif" width="300px"></div>

#### Assignment Link: [https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa1](https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa3)

## Implementation details

### Design patterns

I have implemented 4 design patterns:
  - Strategy
  - Singleton
  - Builder
  - Memento

##### Strategy
I implemented strategy for simulating the player. I made a different play strategy for different type of audio items (songs, collection of songs, podcasts).

I also used Strategy for the wrapped statistics. Each user subclass has a different wrapped method for computing their statistics.

##### Singleton
My library where all the audio files and users are stored is Singleton because it is a unique entity and should be treated as such.

##### Builder
I have implemented builder for formatting the output for many commands.
Many commands almost identical outputs, maybe with slight differences so I created a class (General Result) that is able to get optional
parameters.

Some technical debt I had in the last 2 stages was that I created a different class for each output which created many redundant classes. It is fixed now.

##### Memento
I utilized Memento twice:
  - remembering the pages that a user has navigated through
  - remembering the state of the music player when the songs are interrupted by an ad.

I needed a way to save the state of objects for later retrieval. My two options were the Command and Memento design pattern.
I think Memento is a better fit for my use case.

### Interactions
I used `.stream()` heavily for computing the statistics in wrapped.
I stored all the song listens in a hashmap contained by the `Listener` class.
A wrapped call for an artist checks the song listens hashmap for each user in order to
get each relevant song listen.

For monetization, I used a similar hashmap approach. I had to make different data structures
that stored songs for premium and non-premium monetization because they could also
interweave(non-premium songs -> buy premium -> ... -> cancel premium -> ad). Each hashmap deletes its content
when the songs are monetized.

Regarding AdBreaks, I decided to "take a snapshot" of the player right before an ad is played, play the ad and then revert the player to its last state.

In order to store the pages that a user has navigated through, I utilized the Memento design pattern and 2 stacks:
one for the previous pages and one for the next pages.

### Notes
I used my own code from the last stage as a starting point.

I used Chat-GPT many times when my functional programming call chains were not working properly.

## Skel Structure

* src/
  * checker/ - checker files
  * fileio/ - contains classes used to read data from the json files
  * main/
      * Main - the Main class runs the checker on your implementation. Add the entry point to your implementation in it. Run Main to test your implementation from the IDE or from command line.
      * Test - run the main method from Test class with the name of the input file from the command line and the result will be written
        to the out.txt file. Thus, you can compare this result with ref.
* input/ - contains the tests and library in JSON format
* ref/ - contains all reference output for the tests in JSON format

<div align="center"><img src="https://tenor.com/view/homework-time-gif-24854817.gif" width="500px"></div>
