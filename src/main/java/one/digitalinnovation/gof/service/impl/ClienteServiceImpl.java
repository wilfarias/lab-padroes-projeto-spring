package one.digitalinnovation.gof.service.impl;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ClienteService;
import one.digitalinnovation.gof.service.ViaCepService;
import one.digitalinnovation.gof.service.exceptions.ClienteNotfoundException;

/**
 * Implementação da <b>Strategy</b> {@link ClienteService}, a qual pode ser
 * injetada pelo Spring (via {@link Autowired}). Com isso, como essa classe é um
 * {@link Service}, ela será tratada como um <b>Singleton</b>.
 * 
 * @author falvojr
 */
@Service
public class ClienteServiceImpl implements ClienteService {

	// Singleton: Injetar os componentes do Spring com @Autowired.
	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private ViaCepService viaCepService;
	
	// Strategy: Implementar os métodos definidos na interface.
	// Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

	@Override
	public Iterable<Cliente> buscarTodos() {
		// Buscar todos os Clientes.
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		// Buscar Cliente por ID.
		Optional<Cliente> objCliente = clienteRepository.findById(id);
		Cliente cliente = objCliente.orElseThrow(() -> new ClienteNotfoundException("Cliente não encontrado"));
		return cliente;
	}

	@Override
	public Cliente inserir(Cliente objCliente) {
		Cliente cliente = checarCepExistente(objCliente);
		return clienteRepository.save(cliente);
	}

	@Override
	public Cliente atualizar(Long id, Cliente clienteAtualizado) {		
		try {
			// Buscar Cliente por ID, caso exista:
			Cliente cliente = clienteRepository.getById(id);
			// Checa se o nome do cliente foi alterado
			if(cliente.getNome() != clienteAtualizado.getNome()) 
				cliente.setNome(clienteAtualizado.getNome());
			// Checa se o endereco do cliente foi alterado
			if(cliente.getEndereco() != clienteAtualizado.getEndereco()) 
				cliente.setEndereco(checarCepExistente(clienteAtualizado).getEndereco());
						
			cliente = clienteRepository.save(cliente);
			return cliente;
			
		} catch (EntityNotFoundException  e) {
			throw new ClienteNotfoundException("Cliente não encontrado");
		}
		
	}

	@Override
	public void deletar(Long id) {		
		// Checa existência do cliente
		if(!clienteRepository.existsById(id))
			throw new ClienteNotfoundException("Cliente não encontrado");
		else
			clienteRepository.deleteById(id); // Deletar Cliente por ID.
	}

	private Cliente checarCepExistente(Cliente cliente) {
		// Verificar se o Endereco do Cliente já existe (pelo CEP).
		String cep = cliente.getEndereco().getCep();
		Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
			// Caso não exista, integrar com o ViaCEP e persistir o retorno.
			Endereco novoEndereco = viaCepService.consultarCep(cep);
			enderecoRepository.save(novoEndereco);
			return novoEndereco;
		});
		cliente.setEndereco(endereco);
		// Retorna Cliente, vinculando o Endereco (novo ou existente).
		return cliente;
	}

}
