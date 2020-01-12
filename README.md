**TestDataSet** is a simple java library that helps to populate tables 
in your relational database when writing unit/integration tests.

## Quick start

1. Create *TestDataSetBuilder* passing provider of *java.sql.Connection* to its constructor
1. Use DSL to define tables, columns, and rows to insert (see example below)
1. Call *build()* which returns an instance of *TestDataSet*
1. In your unit test call *TestDataSet.load()* to populate the database with test data

#### Notes
* You are responsible for creating transactions and rolling them back at the 
end of the test.
```java
connection.setAutoCommit(false);
// ... 
connection.rollback();
```

* When using Spring framework you can simply create a connection using 
*org.springframework.jdbc.datasource.DataSourceUtils* and transactions will 
automatically be started and rolled back at the end of the test.  

#### Example
```java
import com.jpragma.testdatasest.TestDataSet;
import com.jpragma.testdatasest.TestDataSetBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Transactional
@ContextConfiguration(classes = {PlayerConfig.class, TestDataSourceConfig.class})
class PlayerRepositoryTest {
    @Autowired
    private PlayerRepository repo;
    @Autowired
    private DataSource dataSource;

    private TestDataSet tds = new TestDataSetBuilder(() -> DataSourceUtils.getConnection(dataSource))
            .table("PLAYER")
                .columns("ID", "FIRST_NAME", "LAST_NAME", "JOINED_ON")
                .row(-1, "Isaac", "Levin", LocalDate.parse("2012-03-01"))
                .build();

    @Test
    void loadExistingPlayer() {
        tds.load();
        Optional<Player> player = repo.findById(-1);
        assertTrue(player.isPresent());
        assertEquals("Isaac", player.get().getFirstName());
    }
}
```
