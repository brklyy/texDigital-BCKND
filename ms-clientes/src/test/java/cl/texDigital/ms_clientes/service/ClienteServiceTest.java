package cl.texDigital.ms_clientes.service;

import cl.texDigital.ms_clientes.dto.ClienteRequestDTO;
import cl.texDigital.ms_clientes.dto.ClienteResponseDTO;
import cl.texDigital.ms_clientes.exception.ResourceNotFoundException;
import cl.texDigital.ms_clientes.model.Cliente;
import cl.texDigital.ms_clientes.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteEjemplo() {
        return new Cliente(1L, "Ana", "Soto", "ana@mail.cl", "+56911111111", "Calle 1", "ACTIVO");
    }

    private ClienteRequestDTO requestEjemplo() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Ana");
        dto.setApellido("Soto");
        dto.setEmail("ana@mail.cl");
        dto.setTelefono("+56911111111");
        dto.setDireccion("Calle 1");
        return dto;
    }

    @Test
    @DisplayName("findById devuelve el cliente cuando existe")
    void findById_devuelveCliente_cuandoExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo()));

        ClienteResponseDTO resultado = clienteService.findById(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo("ana@mail.cl");
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("findById lanza ResourceNotFoundException cuando no existe")
    void findById_lanzaNotFound_cuandoNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findAll mapea todas las entidades a DTOs")
    void findAll_devuelveListaDeDtos() {
        when(clienteRepository.findAll()).thenReturn(List.of(clienteEjemplo()));

        List<ClienteResponseDTO> resultado = clienteService.findAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("create guarda el cliente y lo deja en estado ACTIVO cuando el email es nuevo")
    void create_guardaClienteActivo_cuandoEmailNuevo() {
        when(clienteRepository.existsByEmail("ana@mail.cl")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEjemplo());

        ClienteResponseDTO resultado = clienteService.create(requestEjemplo());

        assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("create lanza IllegalStateException cuando el email ya existe")
    void create_lanzaIllegalState_cuandoEmailDuplicado() {
        when(clienteRepository.existsByEmail("ana@mail.cl")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.create(requestEjemplo()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ana@mail.cl");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("update modifica los datos cuando el cliente existe y el email no cambia")
    void update_modificaCliente_cuandoExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo()));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        ClienteRequestDTO dto = requestEjemplo();
        dto.setNombre("Ana Maria");

        ClienteResponseDTO resultado = clienteService.update(1L, dto);

        assertThat(resultado.getNombre()).isEqualTo("Ana Maria");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("update lanza ResourceNotFoundException cuando el cliente no existe")
    void update_lanzaNotFound_cuandoNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.update(99L, requestEjemplo()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("update lanza IllegalStateException cuando el nuevo email ya pertenece a otro cliente")
    void update_lanzaIllegalState_cuandoNuevoEmailDuplicado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo()));
        when(clienteRepository.existsByEmail("otro@mail.cl")).thenReturn(true);
        ClienteRequestDTO dto = requestEjemplo();
        dto.setEmail("otro@mail.cl");

        assertThatThrownBy(() -> clienteService.update(1L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("otro@mail.cl");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete elimina cuando el cliente existe")
    void delete_eliminaCliente_cuandoExiste() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        clienteService.delete(1L);

        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFoundException cuando el cliente no existe")
    void delete_lanzaNotFound_cuandoNoExiste() {
        when(clienteRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> clienteService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(clienteRepository, never()).deleteById(any());
    }
}
