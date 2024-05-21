package mate.academy.bookshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode(exclude = {"user", "cartItems"})
@ToString(exclude = {"user", "cartItems"})
@Table(name = "shopping_carts")
@NamedEntityGraph(
        name = "shoppingCartWithItemsAndBooks",
        attributeNodes = {
                @NamedAttributeNode(value = "user"),
                @NamedAttributeNode(value = "cartItems", subgraph = "cartItemsGraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "cartItemsGraph",
                        attributeNodes = @NamedAttributeNode(value = "book",
                                subgraph = "bookCategories")
                ),

                @NamedSubgraph(
                        name = "bookCategories",
                        attributeNodes = @NamedAttributeNode(value = "categories")
                )
        }
)
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "shoppingCart")
    private Set<CartItem> cartItems;
}
