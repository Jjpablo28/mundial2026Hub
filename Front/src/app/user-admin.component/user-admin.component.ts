import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {UserAdminService, UserDTO} from './User admin.service';

@Component({
  selector: 'app-user-admin',
  standalone: false,
  templateUrl: './user-admin.component.html',
  styleUrl: './user-admin.component.scss',
})
export class UserAdminComponent implements OnInit {

  users: UserDTO[] = [];
  filteredUsers: UserDTO[] = [];
  isLoading = false;

  searchQuery = '';
  filterRole = '';
  filterStatus = '';

  // Modal edición
  showEditModal = false;
  editingUser: Partial<UserDTO> = {};

  // Modal rol
  showRoleModal = false;
  roleTarget: UserDTO | null = null;
  newRole = '';

  // Modal confirmación eliminar
  showDeleteModal = false;
  deleteTarget: UserDTO | null = null;

  // Popups
  showSuccessPopup = false;
  showErrorPopup = false;
  successMessage = '';
  errorMessage = '';

  isSaving = false;

  readonly roles = ['USER', 'ADMIN'];

  constructor(
    private userAdminService: UserAdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userAdminService.getAllUsers().subscribe({
      next: (res) => {
        this.users = res;
        this.applyFilters();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.showError('Error al cargar los usuarios.');
      }
    });
  }

  applyFilters(): void {
    const q = this.searchQuery.toLowerCase();
    this.filteredUsers = this.users.filter(u => {
      const matchSearch = !q ||
        u.name?.toLowerCase().includes(q) ||
        u.userName?.toLowerCase().includes(q) ||
        u.email?.toLowerCase().includes(q);
      const matchRole = !this.filterRole || u.role === this.filterRole;
      const matchStatus = this.filterStatus === ''
        ? true
        : this.filterStatus === 'enabled'
          ? u.enabled
          : !u.enabled;
      return matchSearch && matchRole && matchStatus;
    });
    this.cdr.detectChanges();
  }

  // ===== EDITAR =====
  openEditModal(user: UserDTO): void {
    this.editingUser = {
      id: user.id,
      name: user.name,
      userName: user.userName,
      email: user.email,
    };
    this.showEditModal = true;
  }

  saveEdit(): void {
    if (!this.editingUser.id) return;
    this.isSaving = true;
    this.userAdminService.updateUser(this.editingUser.id, this.editingUser).subscribe({
      next: (updated) => {
        const idx = this.users.findIndex(u => u.id === updated.id);
        if (idx !== -1) this.users[idx] = updated;
        this.applyFilters();
        this.showEditModal = false;
        this.isSaving = false;
        this.showSuccess('Usuario actualizado correctamente.');
      },
      error: (err) => {
        this.isSaving = false;
        this.showError(err?.error?.message || 'Error al actualizar el usuario.');
      }
    });
  }

  // ===== ROL =====
  openRoleModal(user: UserDTO): void {
    this.roleTarget = user;
    this.newRole = user.role;
    this.showRoleModal = true;
  }

  saveRole(): void {
    if (!this.roleTarget) return;
    this.isSaving = true;
    this.userAdminService.updateRole(this.roleTarget.id, this.newRole).subscribe({
      next: (updated) => {
        const idx = this.users.findIndex(u => u.id === updated.id);
        if (idx !== -1) this.users[idx].role = updated.role;
        this.applyFilters();
        this.showRoleModal = false;
        this.isSaving = false;
        this.showSuccess(`Rol de ${this.roleTarget!.userName} cambiado a ${this.newRole}.`);
        this.roleTarget = null;
      },
      error: (err) => {
        this.isSaving = false;
        this.showError(err?.error?.message || 'Error al cambiar el rol.');
      }
    });
  }

  // ===== HABILITAR / DESHABILITAR =====
  toggleEnabled(user: UserDTO): void {
    const newState = !user.enabled;
    this.userAdminService.toggleEnabled(user.id, newState).subscribe({
      next: () => {
        user.enabled = newState;
        this.applyFilters();
        this.showSuccess(`Usuario ${newState ? 'habilitado' : 'deshabilitado'} correctamente.`);
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Error al cambiar el estado.');
      }
    });
  }

  // ===== ELIMINAR =====
  openDeleteModal(user: UserDTO): void {
    this.deleteTarget = user;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    if (!this.deleteTarget) return;
    this.isSaving = true;
    this.userAdminService.deleteUser(this.deleteTarget.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== this.deleteTarget!.id);
        this.applyFilters();
        this.showDeleteModal = false;
        this.isSaving = false;
        this.showSuccess(`Usuario ${this.deleteTarget!.userName} eliminado.`);
        this.deleteTarget = null;
      },
      error: (err) => {
        this.isSaving = false;
        this.showError(err?.error?.message || 'Error al eliminar el usuario.');
      }
    });
  }

  // ===== POPUPS =====
  showSuccess(msg: string): void {
    this.successMessage = msg;
    this.showSuccessPopup = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showSuccessPopup = false; this.cdr.detectChanges(); }, 4000);
  }

  showError(msg: string): void {
    this.errorMessage = msg;
    this.showErrorPopup = true;
    this.cdr.detectChanges();
    setTimeout(() => { this.showErrorPopup = false; this.cdr.detectChanges(); }, 5000);
  }

  getRoleBadge(role: string): string {
    return role === 'ADMIN' ? 'badge-admin' : 'badge-user';
  }
}
