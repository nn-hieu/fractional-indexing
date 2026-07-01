# Fractional Indexing for Java

A lightweight, zero-dependency Java library for generating lexicographically sortable fractional indices.

Fractional indexing lets you insert or reorder items without renumbering adjacent records, making it ideal for drag-and-drop UIs, Kanban boards, playlists, course sections, and any user-defined ordering.

Common use cases include:
* Kanban boards
* Drag-and-drop lists
* Course sections and curriculums
* Playlists
* Dynamically ordered database records

## Table of Contents

* [Why Fractional Indexing?](#why-fractional-indexing)
* [Features](#features)
* [Requirements](#requirements)
* [Installation](#installation)
* [How It Works](#how-it-works)
* [Usage](#usage)
  * [1. Initialization](#1-initialization)
  * [2. Generating Indices](#2-generating-indices)
  * [3. Real-world Drag & Drop Example](#3-real-world-drag--drop-example)
  * [4. Custom Alphabets](#4-custom-alphabets)
* [Validation](#validation)
* [Rebalancing (Handling Long Indices)](#rebalancing-handling-long-indices)
  * [When to Rebalance](#when-to-rebalance)
* [Complexity](#complexity)
* [FAQ](#faq)
* [License](#license)

## Why Fractional Indexing?

When managing ordered lists in a database, traditional integer-based ordering forces you to update multiple records whenever an item is inserted or moved.

**Traditional Ordering**
```text
1  - Item A
2  - Item B
3  - Item C

(Insert between Item A and Item B)
↓
1  - Item A
2  - New Item (Inserted)
3  - Item B (Needs update)
4  - Item C (Needs update)
```
This causes a cascading update across many rows, degrading performance in large datasets.

**Fractional Indexing**
```text
A  - Item A
B  - Item B
C  - Item C

(Insert between Item A and Item B)
↓
A   - Item A
AN  - New Item (Generated index)
B   - Item B
C   - Item C
```
With fractional indexing, you generate a new index strictly for the inserted item. **Zero adjacent rows need to be updated.**

## Features

* **Zero-Dependency**: Pure Java implementation. No external libraries are required.
* **Framework Agnostic**: Integrates seamlessly with Spring Boot, Quarkus, or plain Java applications.
* **Flexible Index Generation**: Generate indices before, after, or between existing indices without reordering the entire collection.
* **Highly Optimized**: Designed to minimize temporary object allocations during index generation.
* **Built-in Rebalancing**: Provides an efficient divide-and-conquer algorithm to regenerate balanced indices when index lengths grow over time.
* **Customizable Alphabets**: Built-in support for multiple alphanumeric subsets and entirely custom character sets.

## Requirements

* Java 17+

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.nnhieu</groupId>
    <artifactId>fractional-indexing</artifactId>
    <version>0.0.1</version> 
</dependency>
```

No additional configuration is required.

*(Note: Replace `0.0.1` with the latest version available on Maven Central).*

## How It Works

Fractional indexing treats strings as positions within an ordered alphabet.

When an index is requested between two existing indices, the library generates a new string that is lexicographically ordered between them.

```text
A
C
```
Requesting `indexer.between("A", "C")` generates `B`.

If the adjacent indices are sequential (e.g., `A` and `B`), the library appends characters to increase precision, much like adding decimal places. Repeated insertions into the exact same gap will gradually increase the string length until a `rebalance()` operation is performed.

## Usage

### 1. Initialization

By default, the indexer uses an alphanumeric alphabet (`0-9`, `A-Z`, `a-z`).

The standard instantiation uses `defaultInstance()`, which is a convenience method functionally identical to calling `FractionalIndexer.builder().build()`.

```java
import io.github.nnhieu.fractionalindexing.api.FractionalIndexer;

FractionalIndexer indexer = FractionalIndexer.defaultInstance();
```

### 2. Generating Indices

Indices are generated as pure Java `String`s. You can natively sort them in memory using `String.compareTo()` or directly at the database level using SQL `ORDER BY`.

```java
// 1. Generate the very first index (Returns the midpoint of the alphabet)
String first = indexer.first(); 
System.out.println("First: " + first); 

// 2. Generate an index strictly after a given index
String second = indexer.after(first);
System.out.println("Second: " + second); 

// 3. Generate an index strictly before a given index
String zeroth = indexer.before(first);
System.out.println("Zeroth: " + zeroth); 

// 4. Generate an index exactly between two indices
String middle = indexer.between(first, second);
System.out.println("Between first and second: " + middle); 

// Lexicographical ordering is guaranteed: zeroth < first < middle < second
```

### 3. Real-world Drag & Drop Example

When an item is dragged and dropped in a user interface, you only need to calculate the index between the new surrounding items and save the moved entity.

```java
// Fetch the adjacent items in the new location
Task previous = previousTask;
Task next = nextTask;

// Generate the new index
String newIndex = indexer.between(
    previous.getIndex(),
    next.getIndex()
);

// Update and save ONLY the moved entity
movedTask.setIndex(newIndex);
repository.save(movedTask);
```

### 4. Custom Alphabets

You can define custom alphabets or use predefined constants via the Builder pattern. The library strictly validates your custom alphabet upon initialization.

```java
import io.github.nnhieu.fractionalindexing.core.Alphabets;

// Using a custom Alphabet
FractionalIndexer customIndexer = FractionalIndexer.builder()
        .alphabet(Alphabets.of("0123456789ABCDEF"))
        .build();

// Using a predefined Alphabet
FractionalIndexer defaultIndexer = FractionalIndexer.builder()
        .alphabet(Alphabets.ALPHANUMERIC)
        .build();
// Other available predefined alphabets include:
// Alphabets.DIGITS
// Alphabets.UPPERCASE
// Alphabets.LOWERCASE
```

## Validation

The library is designed to fail fast to prevent sorting corruption. Operations will throw an `IllegalArgumentException` or `InvalidIndexException` under the following conditions:

* **Null or Empty Index**: Indices passed to `before()`, `after()` or `between()` cannot be null or blank.
* **Invalid Ordering**: In `between(left, right)`, `left` must be strictly less than `right`.
* **Invalid Characters**: The provided string contains characters not present in the configured `Alphabet`.
* **Invalid Boundaries**: An index cannot end with the absolute minimum or absolute maximum character of the alphabet, as this mathematically restricts further fractional divisions.

## Rebalancing (Handling Long Indices)

Because fractional indices append characters to increase precision, elements repeatedly inserted into tight mathematical spaces will naturally cause indices to grow longer over time.

To prevent strings from becoming excessively large (which may affect sorting performance for very large datasets), the library provides a `rebalance(int size)` method.

### When to Rebalance
Rebalancing is an application-layer concern. You should monitor index lengths (e.g., triggering a background/async job if an index exceeds 20 characters). Rebalancing preserves the exact current ordering of your items while generating a fresh, compact, and perfectly spaced set of indices.

```java
// Example: Rebalancing a list of entities in your application
int totalItems = myEntities.size();

// 1. Generate a perfectly balanced list of short new indices
List<String> balancedIndices = indexer.rebalance(totalItems);

// 2. Assign the new indices back to your entities 
// IMPORTANT: Ensure your list is strictly sorted by the OLD index first!
for (int i = 0; i < totalItems; i++) {
    myEntities.get(i).setIndex(balancedIndices.get(i));
}

// 3. Save all updated entities to the database (Batch Update)
repository.saveAll(myEntities);
```

## Complexity

| Operation | Time Complexity |
| --------- | --------------- |
| `first()` | `O(1)`          |
| `after()` | `O(n)`          |
| `before()` | `O(n)`          |
| `between()` | `O(n)`          |
| `rebalance()` | `O(S * n)`      |

*(Note: `n` represents the length of the string index being processed, and `S` represents the `size` of the list being rebalanced).*

## FAQ

**Do indices grow forever?**
If you continuously insert items into the exact same gap, the index length will increase to accommodate the required precision. Running `rebalance()` resets this entirely.

**When should I rebalance?**
When strings become unnecessarily large for your database configuration (e.g., > 15-20 characters). For context, a fully balanced list of 1,000,000 items only requires about 4 to 5 characters per index.

**Can I store indices in SQL?**
Yes. Store them in a standard `VARCHAR` column and query them using `ORDER BY index_column ASC`.

**Is the ordering deterministic?**
Yes. The generated indices preserve strict lexicographical ordering.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.