import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	public Main() {
		super("New Window");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel world = new Sheet();
		add(world);

		pack();
		setVisible(true);
		setLocationRelativeTo(null);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			new Main();
		});
	}
}